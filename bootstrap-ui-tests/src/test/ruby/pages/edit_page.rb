class EditPage < SitePrism::Page
    url = ENV['BOOTSTRAP_URL'] || 'localhost:8080'

	
	element :message_field, "textarea"
	element :edit_button, "input[text()='Update message']"
	element :cancel_button, "input[text()='Cancel']"
	
	def edit(new_message)
		wait_for_cancel_button 
		message_field.set new_message
		edit_button.click
	end
end