class MessagesPage < SitePrism::Page
    url = ENV['BOOTSTRAP_URL'] || 'localhost:8080'

	set_url "http://#{ url }/#/"
	
	element :message_field, "textarea"
	element :send_button, "input[type=submit]"
	element :logout_link, "a[text()='Logout']"
	
	def send_message(message)
		message_field.set(message)
		send_button.click
	end
	
	def delete_message(message)
		find(:xpath, "//p[text()='#{message}']/following-sibling::span").click_on("Delete")
	end
	
	def edit_message(message)
		find(:xpath, "//p[text()='#{message}']/following-sibling::span").click_on("Edit")
	end
	
	def logout()
		logout_link.click
	end
	
end