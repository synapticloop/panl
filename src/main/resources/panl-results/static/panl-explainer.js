$(document).ready(function() {
	var availableCollections = $("#available_collections");
	for (const collectionUrl of collections) {
		availableCollections.append("[<a href=\"/panl-results-explainer" + collectionUrl + "\">" + collectionUrl + "</a>]&nbsp;");
	}

	// test to see whether we are ready to invoke the panl search service
	var uris = window.location.pathname.split("/");
	if(uris.length >= 4) {
		// we have a collection, and field set
		var collection = uris[2];
		var fieldset = uris[3];
		if(isValidUrl(collection, fieldset)) {
			$("#collection").append("/" + collection + "/" + fieldset);
//			panlSearch();
		}
	}

	$("button#explain").on("click", function (e) {
		var text = $("#uris").val();
		if(text.length == 0) {
			return;
		}
		if(!text.startsWith("/")) {
			text = "/" + text;
		}

		var explainUrl = "/panl-results-explainer/explain" + $("#collection").text() + text;
		console.log(explainUrl);

		// send of the query
		$.ajax({
			url:explainUrl,
			success: function (panlJsonData) {
				populateExplainResults(panlJsonData);
			}
		});
		e.preventDefault();
	});
});

function isValidUrl(collection, fieldset) {
	var fullUrl = "/" + collection + "/" + fieldset;
	for (const collectionUrl of collections) {
		if(collectionUrl === fullUrl) {
			return(true);
		}
	}

	return(false);
}

function populateExplainResults(panlJsonData) {
	console.log(panlJsonData);
//	for(const explanation of panlJsonData.explanation) {
//		console.log(panlJsonData);
//		$("#documents").append("<p>" + explanation + "</p>")
//	}
}


