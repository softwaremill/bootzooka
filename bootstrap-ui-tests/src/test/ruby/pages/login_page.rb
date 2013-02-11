class LoginPage < SitePrism::Page
    url = ENV['BOOTSTRAP_URL'] || 'localhost:8080'

	set_url "http://#{ url }/#/login"
	
	element :login_field, "#login"
	element :password_field, "#password"
	element :login_button, "button[type=submit]"
	element :login_link, "a[text()='Login']"
	
	def open()
		login_link.click
	end
	
	def login(user, pass)
		login_field.set user
		password_field.set pass
		login_button.click
	end
	
	def loginTestUser()
		login("kinga", "12qwQW!@")
	end
end