require 'capybara'
require 'capybara/dsl'
require 'selenium-webdriver'
require 'site_prism'
require 'capybara/rspec'
require 'capybara-screenshot'

require "#{File.dirname(__FILE__)}/pages/app"

include Capybara::DSL
Capybara.default_driver = :selenium
Capybara.default_wait_time = 15

msg_text="capybara test " + [*('A'..'Z')].sample(10).join

describe "send message test" do
   before do
       @app = App.new
	   @app.login_page.load
	   @app.login_page.login("kinga", "12qwQW!@")
   end

   it "Should send message" do
      @app.messages_page.send_message(msg_text)
	  @app.messages_page.should have_content(msg_text)
   end

   after do
      @app.messages_page.logout
	  @app.messages_page.should have_no_content('Logged in as kinga')
    end
end



