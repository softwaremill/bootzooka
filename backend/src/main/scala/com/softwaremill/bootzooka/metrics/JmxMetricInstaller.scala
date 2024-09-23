package com.softwaremill.bootzooka.metrics

import io.opentelemetry.instrumentation.jmx.engine.{JmxMetricInsight, MetricConfiguration}
import io.opentelemetry.instrumentation.jmx.yaml.RuleParser
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.opentelemetry.sdk.autoconfigure.internal.AutoConfigureUtil
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties

import java.nio.file.{Files, Paths}
import java.time.Duration
import scala.jdk.CollectionConverters.*

// Copied & adjusted from: https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/instrumentation/jmx-metrics/javaagent/src/main/java/io/opentelemetry/instrumentation/javaagent/jmx/JmxMetricInsightInstaller.java
// TODO: remove when jmx integration is available as a library: https://github.com/open-telemetry/opentelemetry-java-instrumentation/issues/12308
object JmxMetricInstaller:
  def initialize(otel: AutoConfiguredOpenTelemetrySdk): Unit =
    val config = AutoConfigureUtil.getConfig(otel)

    if config.getBoolean("otel.jmx.enabled", true) then
      val service = JmxMetricInsight.createService(otel.getOpenTelemetrySdk(), beanDiscoveryDelay(config).toMillis())
      val conf = buildMetricConfiguration(config)
      service.start(conf)

  private def beanDiscoveryDelay(configProperties: ConfigProperties): Duration =
    val discoveryDelay = configProperties.getDuration("otel.jmx.discovery.delay")
    if discoveryDelay != null then discoveryDelay
    else
      // If discovery delay has not been configured, have a peek at the metric export interval.
      // It makes sense for both of these values to be similar.
      configProperties.getDuration("otel.metric.export.interval", Duration.ofMinutes(1))

  private def resourceFor(platform: String): String = "/jmx/rules/" + platform + ".yaml"

  private def addRulesForPlatform(platform: String, conf: MetricConfiguration): Unit =
    val yamlResource = resourceFor(platform)
    try
      val inputStream = Option(this.getClass.getResourceAsStream(yamlResource))

      inputStream match
        case Some(stream) =>
          JmxMetricInsight.getLogger.log(java.util.logging.Level.FINE, s"Opened input stream $yamlResource")
          val parserInstance = RuleParser.get()
          parserInstance.addMetricDefsTo(conf, stream, platform)
          stream.close() // Ensure stream is closed
        case None =>
          JmxMetricInsight.getLogger.log(java.util.logging.Level.INFO, s"No support found for $platform")
    catch
      case e: Exception =>
        JmxMetricInsight.getLogger.warning(e.getMessage)

  private def buildFromDefaultRules(conf: MetricConfiguration, configProperties: ConfigProperties): Unit =
    val platforms = configProperties.getList("otel.jmx.target.system")

    platforms
      .iterator()
      .asScala
      .foreach: platform =>
        addRulesForPlatform(platform, conf)

  private def buildFromUserRules(conf: MetricConfiguration, configProperties: ConfigProperties): Unit =
    val configFiles = configProperties.getList("otel.jmx.config").asScala

    configFiles.foreach: configFile =>
      JmxMetricInsight.getLogger.log(java.util.logging.Level.FINE, s"JMX config file name: $configFile")

      val parserInstance = RuleParser.get()

      try
        val inputStream = Files.newInputStream(Paths.get(configFile))
        try
          parserInstance.addMetricDefsTo(conf, inputStream, configFile)
        finally
          inputStream.close()
      catch
        case e: Exception =>
          // yaml parsing errors are caught and logged inside of addMetricDefsTo
          // only file access related exceptions are expected here
          JmxMetricInsight.getLogger.warning(e.toString)

  private def buildMetricConfiguration(configProperties: ConfigProperties): MetricConfiguration =
    val metricConfiguration = new MetricConfiguration()
    buildFromDefaultRules(metricConfiguration, configProperties)
    buildFromUserRules(metricConfiguration, configProperties)
    addRulesForPlatform("jvm", metricConfiguration)
    metricConfiguration
