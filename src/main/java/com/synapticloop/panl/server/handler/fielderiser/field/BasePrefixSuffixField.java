package com.synapticloop.panl.server.handler.fielderiser.field;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public abstract class BasePrefixSuffixField extends BaseField {
	public static final String PROPERTY_KEY_SUFFIX_PREFIX = ".prefix";
	public static final String PROPERTY_KEY_SUFFIX_SUFFIX = ".suffix";
	protected boolean hasValuePrefix = false;
	protected boolean hasValueSuffix = false;
	protected String valuePrefix;
	protected String valueSuffix;

	/**
	 *
	 * @param lpseCode
	 * @param propertyKey
	 * @param properties
	 * @param solrCollection
	 * @param lpseLength
	 * @throws PanlServerException
	 */
	public BasePrefixSuffixField(String lpseCode, String propertyKey, Properties properties, String solrCollection, String panlCollectionUri, int lpseLength) throws PanlServerException {
		super(lpseCode, propertyKey, properties, solrCollection, panlCollectionUri, lpseLength);

		populateSuffixAndPrefix();
	}

	public BasePrefixSuffixField(String lpseCode, String propertyKey, Properties properties, String solrCollection, String panlCollectionUri) throws PanlServerException {
		super(lpseCode, propertyKey, properties, solrCollection, panlCollectionUri, 1);

		populateParamSuffixAndPrefix(propertyKey);
	}

	protected void populateParamSuffixAndPrefix(String propertyKey) {
		this.valuePrefix = properties.getProperty(propertyKey + PROPERTY_KEY_SUFFIX_PREFIX);

		this.valueSuffix = properties.getProperty(propertyKey + PROPERTY_KEY_SUFFIX_SUFFIX);

		checkPrefixSuffix();
	}

	protected void populateSuffixAndPrefix() {
		this.valuePrefix = properties.getProperty(PROPERTY_KEY_PANL_PREFIX + lpseCode);

		this.valueSuffix = properties.getProperty(PROPERTY_KEY_PANL_SUFFIX + lpseCode);

		checkPrefixSuffix();
	}

	private void checkPrefixSuffix() {
		if (this.valuePrefix != null && !this.valuePrefix.isEmpty()) {
			hasValuePrefix = true;
		}

		if (this.valueSuffix != null && !this.valueSuffix.isEmpty()) {
			hasValueSuffix = true;
		}
	}

	public String getValuePrefix() {
		if (hasValuePrefix) {
			return (valuePrefix);
		} else {
			return ("");
		}
	}

	public String getValueSuffix() {
		if (hasValueSuffix) {
			return (valueSuffix);
		} else {
			return ("");
		}
	}

	/**
	 * <p>The panl value (from the URI) can have a prefix or suffix, or both
	 * applied to it.</p>
	 *
	 * <p>Remove any suffixes, or prefixes from a URI parameter, should they be
	 * defined for the LPSE code.</p>
	 *
	 * <p>Additionally, if this is a boolean field, it may be that there also is
	 * a replacement for true/false for it.</p>
	 *
	 * <p>This will also validate the value by the Solr field type, at the
	 * moment, the only fields that have additional validation are:</p>
	 *
	 * <ul>
	 *   <li>"solr.IntPointField"</li>
	 *   <li>"solr.LongPointField"</li>
	 *   <li>"solr.DoublePointField"</li>
	 *   <li>"solr.FloatPointField"</li>
	 * </ul>
	 *
	 * @param value the value to convert if any conversions are required
	 *
	 * @return the de-suffixed, de-prefixed, and de-replaced value.  This will
	 * 		return <code>null</code> if it is invalid.
	 */
	@Override public String getDecodedValue(String value) {
		if(null == value) {
			return(null);
		}

		String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);

		if (hasValuePrefix) {
			if (decodedValue.startsWith(valuePrefix)) {
				decodedValue = decodedValue.substring(valuePrefix.length());
			} else {
				return (null);
			}
		}

		if (hasValueSuffix) {
			if (decodedValue.endsWith(valueSuffix)) {
				decodedValue = decodedValue.substring(0, decodedValue.length() - valueSuffix.length());
			} else {
				return (null);
			}
		}

		// now we are going to validate the fields, boolean and string fields have
		// their own validation
		String validatedValue = getValidatedValue(decodedValue);
		if (null != validatedValue && validatedValue.isBlank()) {
			return (null);
		} else {
			return validatedValue;
		}
	}

	@Override public String getEncodedPanlValue(String value) {
		StringBuilder sb = new StringBuilder();

		if (hasValuePrefix) {
			sb.append(valuePrefix);
		}

		sb.append(value);

		if (hasValueSuffix) {
			sb.append(valueSuffix);
		}

		return (URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8));
	}

	@Override public String getEncodedPanlValue(LpseToken lpseToken) {
		if (null == lpseToken.getValue()) {
			return ("");
		} else {
			return(getEncodedPanlValue(lpseToken.getValue()));
		}
	}

	@Override protected void appendToAvailableObjectInternal(JSONObject jsonObject) {

	}
}
