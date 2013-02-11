class EditPage < SitePrism::Page
    url = ENV['BOOTSTRAP_URL'] || 'localhost:8080'
	
	set_url "http://#{ url }/#/entry/.*"

	element :message_field, "textarea"
	element :edit_button, "input[value='Update message']"
	element :cancel_button, "input[value='Cancel']"
	
	def edit(new_message)
		message_field.set new_message
		edit_button.click
	end
end