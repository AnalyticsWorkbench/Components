function main(form, state, metaByInput) {
	var fileType = state.output === 'Excel file' ? 'xls' : 'sdt';
	return {
		fileType: fileType,
		dataType: 'SequentialPatterns'
	};
}