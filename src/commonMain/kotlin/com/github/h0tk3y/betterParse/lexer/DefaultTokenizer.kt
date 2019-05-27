package com.github.h0tk3y.betterParse.lexer

private fun Regex.countGroups() = "(?:$pattern)?".toRegex().find("")!!.groups.size - 1

/** Tokenizes input character sequences using the [tokens], prioritized by their order in the list,
 * first matched first. */
class DefaultTokenizer(override val tokens: List<Token>) : Tokenizer {
    init {
        require(tokens.isNotEmpty()) { "The tokens list should not be empty" }
        tokens.forEach { it.tokenizer = this }
    }

    /** Tokenizes the [input] from a [String] into a [TokenMatchesSequence]. */
    override fun tokenize(input: String): TokenMatchesSequence = tokenize(input as CharSequence)

    /** Tokenizes the [input] from a [Scanner] into a [TokenMatchesSequence]. */
    fun tokenize(input: CharSequence) = TokenMatchesSequence(DefaultTokenProducer(tokens, input), this)
}

private class DefaultTokenProducer(private val tokens: List<Token>, private val input: CharSequence) : TokenProducer {
    private val inputLength = input.length
    private var tokenIndex = 0
    private var pos = 0
    private var row = 1
    private var col = 1

    private var errorState = false

    override fun nextToken(): TokenMatch? {
        if (pos > input.lastIndex || errorState) {
            return null
        }

        for (index in 0 until tokens.size) {
            val token = tokens[index]
            val matchLength = token.match(input, pos)
            if (matchLength == 0)
                continue

            val result = TokenMatch(token, tokenIndex++, input, pos, matchLength, row, col)

            for (i in pos until pos + matchLength) {
                if (input[i] == '\n') {
                    row++
                    col = 1
                } else {
                    col++
                }
            }

            pos += matchLength

            return result
        }

        errorState = true
        return TokenMatch(noneMatched, tokenIndex++, input, pos, inputLength - pos, row, col)
    }
}