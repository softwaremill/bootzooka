String getDockerTag() {
    return sh(script: 'git describe --always --tags', returnStdout: true)?.trim()
}

String getBranch() {
    return sh(script: 'git describe --all', returnStdout: true).drop(15).replaceAll('/','_')
}

def serviceAccount = "sml-internal-jenkins"
def label = "build-${UUID.randomUUID().toString()}"
podTemplate(label: label, yaml: """
apiVersion: v1
kind: Pod
spec:
  serviceAccountName: ${serviceAccount}
  volumes:
    - name: ivy-cache
      emptyDir: {}
    - name: sbt-cache
      emptyDir: {}
    - name: dind-storage
      emptyDir: {}
  containers:
  - name: sbt
    image: tomkrol/sbt-alpine-yarn:1.3.13
    imagePullPolicy: Always
    command:
    - cat
    tty: true
    resources:
      requests:
        memory: "4Gi"
      limits:
        memory: "8Gi"
        cpu: "1"
  - name: dind
    image: tomkrol/dind-sbt-alpine:18.09
    imagePullPolicy: Always
    resources:
      requests:
        memory: "1Gi"
      limits:
        cpu: "1"
        memory: "8Gi"
    securityContext:
      privileged: true
    volumeMounts:
      - name: dind-storage
        mountPath: /var/lib/docker
  - name: usage
    image: lachlanevenson/k8s-helm:v2.14.0
    tty: true
    command:
    - cat
    resources:
      requests:
        memory: "256Mi"
        cpu: "0.1"
      limits:
        cpu: "0.5"
        memory: "512Mi"
    volumeMounts:
      - name: dind-storage
        mountPath: /var/lib/docker
"""
) {
    timeout(50) {
        node(label) {
            try {
                ansiColor('xterm') {
                    stage('Checkout') {
                        checkout scm
                        dockerTag = getDockerTag()
                        gitBranch = getBranch()
                    }
                    container('sbt') {
                        stage('Test') {
                            sh """
                                set +e
                                sbt -J-Xmx3072M test
                            """
                        }
                    }
                    if (env.BRANCH_NAME == 'master') {
                      container('dind') {
                        stage('Build docker image') {
                          withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', passwordVariable: 'DOCKERHUB_PASSWORD', usernameVariable: 'DOCKERHUB_USERNAME')]) {
                            sh """
                                set +e
                                docker login -u \${DOCKERHUB_USERNAME} -p \${DOCKERHUB_PASSWORD}
                                sbt docker:publishLocal
                                docker images|grep bootzooka|grep -v latest|awk \'{print \$2}\' > /var/lib/docker/dockertag.txt
                                docker push softwaremill/bootzooka:\$(cat /var/lib/docker/dockertag.txt)
                            """
                          }
                        }
                      }
                    }
                    if (env.BRANCH_NAME == 'master') {
                      container('usage') {
                        stage('Deploy') {
                          withCredentials([string(credentialsId: 'bootzooka.smtp', variable: 'BOOTZOOOKA_SMTP_PASSWORD'),
                                          string(credentialsId: 'bootzooka.sql', variable: 'BOOTZOOOKA_DB_PASSWORD')]) {
                            sh """
                                helm upgrade --reuse-values --set image.tag=\$(cat /var/lib/docker/dockertag.txt)  --set bootzooka.smtp.passwrod=${BOOTZOOOKA_SMTP_PASSWORD} --set bootzooka.sqk.password=${BOOTZOOOKA_DB_PASSWORD} bootzooka ./helm/bootzooka
                            """
                          }
                        }
                      }
                    }
                }
            } catch (e) {
                currentBuild.result = 'FAILED'
                throw e
            } finally {
                if (currentBuild.result == "FAILURE") {
                    color = "#FF0000"
                } else {
                    color = "#00FF00"
                }
            }
        }
    }
}