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
		}
	}

	$("button#explain").on("click", function (event) {
		event.preventDefault();
		panlExplain();
	});
});

function panlExplain() {
	var text = $("#uris").val();
	if(text.length == 0) {
		return;
	}
	if(!text.startsWith("/")) {
		text = "/" + text;
	}

	var explainUrl = "/panl-results-explainer/explain" + $("#collection").text() + text;


	$.ajax({
		url:explainUrl,
		success: function (panlJsonData) {
			populateExplainResults(panlJsonData);
	  }
	});
}

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
	$("#documents").append("<h1>Request Explainer</h1>");
	for(const expl of panlJsonData.explanation) {
		$("#documents").append("<pre class=\"explain\">" + escapeHTML(expl) + "</pre>")
	}

	$("#documents").append("<h1>Configuration Explainer</h1>");
	for(const conf of panlJsonData.configuration) {
		var innerPre = "";
		for(const inner of conf) {
			innerPre = innerPre + escapeHTML(inner) + "\n";
		}
		$("#documents").append("<pre class=\"explain\">" + innerPre + "</pre>")
	}
}

function escapeHTML(text) {
   return text.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}


