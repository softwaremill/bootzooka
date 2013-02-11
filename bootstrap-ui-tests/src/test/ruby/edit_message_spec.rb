require 'capybara'
require 'capybara/dsl'
require 'selenium-webdriver'
require 'site_prism'
require 'capybara/rspec'
require 'capybara-screenshot'

require "#{File.dirname(__FILE__)}/pages/app"

Capybara.default_driver = :selenium
Capybara.default_wait_time = 30

msg_text="capybara test " + [*('A'..'Z')].sample(10).join
new_msg_txt="changed " + [*('A'..'Z')].sample(10).join

describe "edit message test" do
   before do
       include Capybara::DSL
	   @app = App.new
	   @app.login_page.load
	   @app.login_page.should be_all_there
   	   @app.wait_until_throbber_invisible	   
	   @app.login_page.loginTestUser
	   @app.messages_page.send_message(msg_text)
   end

   it "Should edit message" do
      sleep(4)
#	  @app.wait_until_throbber_invisible
      @app.messages_page.should have_content(msg_text)
	  
	  @app.messages_page.edit_message(msg_text)
	  sleep(4)  
#	  @app.wait_until_throbber_invisible
	  
	  @app.edit_page.edit(new_msg_txt)
	  sleep(4)
#	  @app.wait_until_throbber_invisible

	  @app.messages_page.should have_no_content(msg_text)
	  @app.messages_page.should have_content(new_msg_txt)
   end

   after do
      @app.messages_page.logout
    end
end