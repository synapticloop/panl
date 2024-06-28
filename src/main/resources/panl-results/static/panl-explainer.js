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
			$("#explain").removeAttr("disabled");
			$("#uris").removeAttr("disabled");

			panlExplain();

			$("button#explain").on("click", function (event) {
				event.preventDefault();
				panlExplain();
			});
		}
	}
});

function panlExplain() {
	var text = $("#uris").val();
	// maybe we will have a query parameter...?

	if(text.length == 0) {
		text = getURLParameter("explain");
		if(text !== undefined && text.length != 0) {
			$("#uris").val(text)
			var url = window.location.href;
      window.history.replaceState({}, "", url.split('?')[0]);
		} else {
			text = "";
		}
	}

	if(text.length == 0) {
		text = "/";
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

function getURLParameter(sParam) {
	var sPageURL = window.location.search.substring(1);
	var sURLVariables = sPageURL.split('&');
	for (var i = 0; i < sURLVariables.length; i++) {
		var sParameterName = sURLVariables[i].split('=');
		if (sParameterName[0] == sParam)  {
			return sParameterName[1];
     }
	}
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
	console.log(panlJsonData);

	$("#documents").empty();
	$("#documents").append("<h1>Request Token Explainer</h1>");
	if(panlJsonData.explanation.length === 0) {
		$("#documents").append("<code class=\"explain\">No URI path entered, nothing to explain.</pre>")
	}

	for(const expl of panlJsonData.explanation) {
		if(expl.includes("[ INVALID ]")) {
			$("#documents").append("<code class=\"explain invalid\">" + escapeHTML(expl) + "</pre>")
		} else {
			$("#documents").append("<code class=\"explain valid\">" + escapeHTML(expl) + "</pre>")
		}
	}

	$("#documents").append("<h1>Configuration Parameters</h1>");
	for(const parameter of panlJsonData.parameters) {
		var innerCode = "<strong>[ " + parameter.value + " ] </strong> ";

		for(const inner of parameter.description) {
			innerCode = innerCode + inner + "<br />";
		}

		 innerCode = innerCode + " <em>(set by the property '" + parameter.property + "')</em>";
		$("#documents").append("<code class=\"explain\">" + innerCode + "</pre>")
	}

	$("#documents").append("<h1>Field Configuration Explainer</h1>");
	$("#documents").append("<p>In order of URI path</p>");
	for(const conf of panlJsonData.configuration) {
		var isFirst = true;
		var innerCode = "";
		for(const inner of conf) {

			innerCode = innerCode + (isFirst ? "" : " - ") +escapeHTML(inner) + "<br />";
			isFirst = false;
		}
		$("#documents").append("<code class=\"explain\">" + innerCode + "</pre>")
	}
}

function escapeHTML(text) {
   return text.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}


