$(document).ready(function() {
	let availableCollections = $("#available_collections");
	let currentCollectionName = "";

	for (const collectionUrl of collections) {
		const lastIndex = collectionUrl.lastIndexOf("/");
		const collectionName = collectionUrl.substring(1, lastIndex);
		const fieldSet = collectionUrl.substring(lastIndex + 1);
		if(currentCollectionName !== collectionName) {
			availableCollections.append("<br />&nbsp;<strong>" + collectionName + "</strong>")
			currentCollectionName = collectionName;
		}

		availableCollections.append("&nbsp;-&nbsp;<a href=\"/panl-results-explainer" + collectionUrl + "\">[&nbsp;" + fieldSet + "&nbsp;]</a>");
	}

	availableCollections.append("<br />")

	$("#collection_toggle").on("click", function (event) {
		$("#collection_holder").toggle();
	});

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
	let text = $("#uris").val();
	// maybe we will have a query parameter...?

	if(text.length === 0) {
		text = getURLParameter("explain");
		if(text !== undefined && text.length != 0) {
			$("#uris").val(text)
			let url = window.location.href;
      window.history.replaceState({}, "", url.split('?')[0]);
		} else {
			text = "";
		}
	}

	if(text.length === 0) {
		text = "/";
	}

	if(!text.startsWith("/")) {
		text = "/" + text;
	}

	let explainUrl = "/panl-results-explainer/explain" + $("#collection").text() + text;

	$.ajax({
		url:explainUrl,
		success: function (panlJsonData) {
			populateExplainResults(panlJsonData);
	  }
	});
}

function getURLParameter(sParam) {
	let sPageURL = window.location.search.substring(1);
	let sURLVariables = sPageURL.split('&');
	for (let i = 0; i < sURLVariables.length; i++) {
		let sParameterName = sURLVariables[i].split('=');
		if (sParameterName[0] === sParam)  {
			return sParameterName[1];
		}
	}
}

function isValidUrl(collection, fieldset) {
	let fullUrl = "/" + collection + "/" + fieldset;
	for (const collectionUrl of collections) {
		if(collectionUrl === fullUrl) {
			return(true);
		}
	}

	return(false);
}

function populateExplainResults(panlJsonData) {
	console.log(panlJsonData);
	let resultDocuments = $("#documents");
	resultDocuments.empty();
	resultDocuments.append("<h1>Request Token Explainer</h1>");
	if(panlJsonData.explanation.length === 0) {
		resultDocuments.append("<code class=\"explain\">No URI path entered, nothing to explain.</pre>")
	}

	for(const expl of panlJsonData.explanation) {
		if(expl.includes("[ INVALID ]")) {
			resultDocuments.append("<code class=\"explain invalid\">" + escapeHTML(expl) + "</pre>")
		} else {
			resultDocuments.append("<code class=\"explain valid\">" + escapeHTML(expl) + "</pre>")
		}
	}

	resultDocuments.append("<h1>Configuration Parameters</h1>");
	for(const parameter of panlJsonData.parameters) {
		let innerCode = "<strong>[ " + parameter.value + " ] </strong> ";

		for(const inner of parameter.description) {
			innerCode = innerCode + inner + "<br />";
		}

		 innerCode = innerCode + " <em>(set by the property '" + parameter.property + "')</em>";
		resultDocuments.append("<code class=\"explain\">" + innerCode + "</pre>")
	}

	resultDocuments.append("<h1>Field Configuration Explainer</h1>");
	resultDocuments.append("<p>In order of URI path</p>");
	for(const conf of panlJsonData.configuration) {
		let isFirst = true;
		let innerCode = "";

		for(const inner of conf) {
			innerCode = innerCode + (isFirst ? "" : " - ") +escapeHTML(inner) + "<br />";
			isFirst = false;
		}
		resultDocuments.append("<code class=\"explain\">" + innerCode + "</pre>")
	}
}

function escapeHTML(text) {
   return text.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}


