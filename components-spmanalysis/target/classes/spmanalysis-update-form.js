function main(form, state, metaByInput) {
	var input = metaByInput.in_1;
	if (!input) return form;

	var fields = input.fields;
	
	return Object.assign({}, form, {
		input_encoding: Object.assign({}, form.input_encoding, {
			options: fields
		}),
		group_by: Object.assign({}, form.group_by, {
			options: fields
		}),
		split_at_property: Object.assign({}, form.split_at_property, {
			options: fields
		}) 
	});
}