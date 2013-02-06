class LoginPage < SitePrism::Page
    url = ENV['BOOTSTRAP_URL'] || 'localhost:8080'

	set_url "http://#{ url }/#/login"
	
	element :login_field, "#login"
	element :password_field, "#password"
	element :login_button, "button[type=submit]"
	
	def login(user, pass)
		wait_for_login_field 
		login_field.set user
		password_field.set pass
		login_button.click
	end
end