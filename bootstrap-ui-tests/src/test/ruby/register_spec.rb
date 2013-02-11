require 'capybara'
require 'capybara/dsl'
require 'selenium-webdriver'
require 'site_prism'
require 'capybara/rspec'
require 'capybara-screenshot'

require "#{File.dirname(__FILE__)}/pages/app"

Capybara.default_driver = :selenium
Capybara.default_wait_time = 30
user = msg_text=[*('A'..'Z')].sample(5).join
email = msg_text=[*('A'..'Z')].sample(5).join + "@example.org"
pass = msg_text=[*('A'..'Z')].sample(8).join

describe "register test" do
  before do
    include Capybara::DSL
    @app = App.new
  end

  it "Should register" do
    @app.register_page.load
    @app.wait_until_throbber_invisible
    @app.register_page.register(user, email, pass)

    @app.messages_page.should have_content("Please wait")
    @app.wait_until_throbber_invisible
    @app.login_page.open

	@app.wait_until_throbber_invisible
    @app.login_page.login(email, pass)

	@app.messages_page.should have_content("Please wait")
	@app.wait_until_throbber_invisible
    @app.messages_page.should have_content("Logged in as #{user}")
  end

  after do
    @app.messages_page.logout
    @app.messages_page.should have_no_content("Logged in as #{user}")
  end
end