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
user = msg_text=[*('A'..'Z')].sample(5).join
email = msg_text=[*('A'..'Z')].sample(5).join + "@example.org"
pass = msg_text=[*('A'..'Z')].sample(8).join

describe "register test" do
   before do
       @app = App.new
   end

   it "Should register" do
      @app.register_page.load
      @app.register_page.should be_all_there
      @app.register_page.register(user, email, pass)
	  
	  
	  @app.messages_page.should have_content("Please wait")	  
	  #wait_until{@app.messages_page.should have_content("User registered successfully")}
	  
	  #sleep(4)
	  
	  click_link 'Login' #@app.messages_page.login_link

      @app.login_page.should be_all_there
      @app.login_page.login(email, pass)

      @app.messages_page.wait_for_logout_link
      @app.messages_page.should have_content("Logged in as #{user}")
   end

   after do
      @app.messages_page.logout
	  @app.messages_page.should have_no_content("Logged in as #{user}")
    end
end



