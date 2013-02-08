require "#{File.dirname(__FILE__)}/login_page"
require "#{File.dirname(__FILE__)}/messages_page"
require "#{File.dirname(__FILE__)}/edit_page"
require "#{File.dirname(__FILE__)}/register_page"



class App < SitePrism::Page
  element :throbber, "#ajaxthrobber"
  def login_page
    LoginPage.new
  end
  
  def register_page
    RegisterPage.new
  end

  def messages_page
      MessagesPage.new
  end

  def edit_page
      EditPage.new
  end

end