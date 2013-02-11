class RegisterPage < SitePrism::Page
    url = ENV['BOOTSTRAP_URL'] || 'localhost:8080'

	set_url "http://#{ url }/#/register"
	
	element :login_field, "#login"
	element :email_field, "#email"
	element :password_field, "#password"
	element :repassword_field, "#repeatPassword"
	element :register_button, "button[type=submit]"
	
	def register(login, email, pass)
		login_field.set login
		email_field.set email
		password_field.set pass
		repassword_field.set pass
		register_button.click
	end
end