package nolambda.linkrouter

import java.util.regex.Pattern

class DeepLinkEntry private constructor(
    private val regex: Pattern,
    private val parameters: Set<String>
) {

    companion object {
        private const val PARAM = "([a-zA-Z][a-zA-Z0-9_-]*)"
        private const val PARAM_REGEX = "%7B($PARAM)%7D"
        private const val PARAM_VALUE = "([a-zA-Z0-9_#'!+%~,\\-\\.\\@\\$\\:]+)"

        private val PARAM_PATTERN = Pattern.compile(PARAM_REGEX)

        fun parse(url: String): DeepLinkEntry {
            val parsedUri = DeepLinkUri.parse(url)
            val schemeHostAndPath = schemeHostAndPath(parsedUri)
            val regex = Pattern.compile(schemeHostAndPath.replace(PARAM_REGEX.toRegex(), PARAM_VALUE) + "$")
            return DeepLinkEntry(regex, parseParameters(parsedUri))
        }

        private fun schemeHostAndPath(uri: DeepLinkUri): String {
            return uri.scheme() + "://" + uri.encodedHost() + uri.encodedPath()
        }

        private fun parseParameters(uri: DeepLinkUri): Set<String> {
            val matcher = PARAM_PATTERN.matcher(uri.encodedHost() + uri.encodedPath())
            val patterns = linkedSetOf<String>()
            while (matcher.find()) {
                patterns.add(matcher.group(1))
            }
            return patterns
        }
    }

    fun matches(inputUri: String): Boolean {
        val deepLinkUri = DeepLinkUri.parse(inputUri)
        return deepLinkUri != null && regex.matcher(schemeHostAndPath(deepLinkUri)).find()
    }

    fun getParameters(inputUri: String): Map<String, String> {
        val deepLinkUri = DeepLinkUri.parse(inputUri)
        val matcher = regex.matcher(schemeHostAndPath(deepLinkUri))
        val paramsMap = mutableMapOf<String, String>()

        var i = 1
        if (matcher.matches()) {
            parameters.forEach { key ->
                val value = matcher.group(i++)
                if (value != null && "" != value.trim { it <= ' ' }) {
                    paramsMap[key] = value
                }
            }
        }
        return paramsMap
    }


}
