require 'capybara'
require 'capybara/dsl'
require 'selenium-webdriver'
require 'site_prism'
require 'capybara/rspec'
require 'capybara-screenshot'

require "#{File.dirname(__FILE__)}/pages/app"

Capybara.default_driver = :selenium
Capybara.default_wait_time = 15

describe "login test" do
   before do
	   include Capybara::DSL
       @app = App.new
   end

   it "Should log in" do
      @app.login_page.load

      @app.login_page.should be_all_there
      @app.login_page.loginTestUser

      @app.messages_page.should have_content('Logged in as kinga')
   end

   after do
      @app.messages_page.logout
	  @app.messages_page.should have_no_content('Logged in as kinga')
    end
end



