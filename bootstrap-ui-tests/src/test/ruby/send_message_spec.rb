require 'capybara'
require 'capybara/dsl'
require 'selenium-webdriver'
require 'site_prism'
require 'capybara/rspec'
require 'capybara-screenshot'

require "#{File.dirname(__FILE__)}/pages/app"

Capybara.default_driver = :selenium
Capybara.default_wait_time = 15

msg_text="capybara test " + [*('A'..'Z')].sample(10).join

describe "send message test" do
   before do
	   include Capybara::DSL
       @app = App.new
	   @app.login_page.load
	   @app.login_page.should be_all_there
	   @app.login_page.loginTestUser
   end

   it "Should send message" do
      @app.messages_page.send_message(msg_text)
	  @app.messages_page.should have_content(msg_text)
   end

   after do
      @app.messages_page.logout
    end
end