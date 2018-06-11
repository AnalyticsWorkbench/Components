function main(form, state, metaByInput) {
	var input = metaByInput.in_1;
	if (!input) return form;

	var fields = input.fields;
	var values = input.values;
	var min = input.dateRange[0];
	var max = input.dateRange[1];

	return Object.assign({}, form, {
		filters: Object.assign({}, form.filters, {
			dependencies: [{
				source: "property",
				target: "value",
				targetProperty: "options",
				map: values 
			}],
			columns: Object.assign({}, form.filters.columns, {
				property: Object.assign({}, form.filters.columns.property, {
					options: fields
				})
			})
		}),
		dateRange: Object.assign({}, form.dateRange, {
			min: min,
			max: max
		})
	});
}
