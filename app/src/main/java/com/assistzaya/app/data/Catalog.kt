package com.assistzaya.app.data

/**
 * CATÁLOGO DE DEMONSTRAÇÃO.
 * Os vídeos abaixo são filmes abertos (Blender Foundation, licença Creative Commons) e clipes
 * de teste do Google. Troque este objeto por uma chamada à sua API quando o backend existir,
 * usando apenas conteúdos que você tenha direito de distribuir.
 */
object Catalog {
    private const val B = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/"

    private fun ep(cid: String, n: Int, file: String) =
        Episode("${cid}e$n", 1, n, "Episódio $n", B + file, "04/10/2026")

    val items: List<Content> = listOf(
        Content(
            "c1", "Big Buck Bunny", ContentType.FILME, listOf("Animação", "Aventura", "Kids"),
            "L", 2008, "DUB", "Um coelho gigante e gentil enfrenta três roedores travessos. Filme aberto de demonstração.",
            null, null, 0xFF2E7D32.toInt(), B + "BigBuckBunny.mp4", emptyList(),
            setOf(TAG_TRENDING, TAG_TOP_TODAY, TAG_POPULAR)
        ),
        Content(
            "c2", "Sintel", ContentType.FILME, listOf("Fantasia", "Ação", "Aventura"),
            "12", 2010, "LEG", "Uma jovem parte em busca de um dragão que se tornou seu amigo. Filme aberto de demonstração.",
            null, null, 0xFF6A1B9A.toInt(), B + "Sintel.mp4", emptyList(),
            setOf(TAG_TRENDING, TAG_TOP_VIEWED, TAG_POPULAR)
        ),
        Content(
            "c3", "Tears of Steel", ContentType.FILME, listOf("Ficção científica", "Ação"),
            "12", 2012, "DUB", "Um grupo de guerreiros e cientistas tenta salvar o mundo de robôs. Filme aberto de demonstração.",
            null, null, 0xFF1565C0.toInt(), B + "TearsOfSteel.mp4", emptyList(),
            setOf(TAG_NEW, TAG_TOP_TODAY)
        ),
        Content(
            "c4", "Elephants Dream", ContentType.FILME, listOf("Animação", "Fantasia", "Ficção científica"),
            "12", 2006, "LEG", "Dois personagens exploram uma máquina estranha e infinita. Filme aberto de demonstração.",
            null, null, 0xFFEF6C00.toInt(), B + "ElephantsDream.mp4", emptyList(),
            setOf(TAG_TOP_VIEWED, TAG_POPULAR)
        ),
        Content(
            "c5", "Demo Anime 1", ContentType.ANIME, listOf("Animação", "Ação"),
            "14", 2026, "LEG", "Série de demonstração do tipo anime, com episódios de teste.",
            null, null, 0xFFC62828.toInt(), null,
            listOf(ep("c5", 1, "ForBiggerBlazes.mp4"), ep("c5", 2, "ForBiggerEscapes.mp4"), ep("c5", 3, "ForBiggerFun.mp4")),
            setOf(TAG_NEW, TAG_TRENDING, TAG_TOP_TODAY)
        ),
        Content(
            "c6", "Demo Série 1", ContentType.SERIE, listOf("Drama", "Mistério"),
            "14", 2026, "DUB", "Série de demonstração com episódios de teste.",
            null, null, 0xFF00695C.toInt(), null,
            listOf(ep("c6", 1, "ForBiggerJoyrides.mp4"), ep("c6", 2, "ForBiggerMeltdowns.mp4"), ep("c6", 3, "SubaruOutbackOnStreetAndDirt.mp4")),
            setOf(TAG_NEW, TAG_TOP_VIEWED, TAG_POPULAR)
        ),
        Content(
            "c7", "Demo Anime 2", ContentType.ANIME, listOf("Fantasia", "Aventura"),
            "12", 2025, "DUAL", "Segundo anime de demonstração, com dois episódios de teste.",
            null, null, 0xFFAD1457.toInt(), null,
            listOf(ep("c7", 1, "WeAreGoingOnBullrun.mp4"), ep("c7", 2, "WhatCarCanYouGetForAGrand.mp4")),
            setOf(TAG_TRENDING, TAG_POPULAR)
        ),
        Content(
            "c8", "Demo Série 2", ContentType.SERIE, listOf("Crime", "Thriller"),
            "16", 2024, "LEG", "Segunda série de demonstração, com dois episódios de teste.",
            null, null, 0xFF37474F.toInt(), null,
            listOf(ep("c8", 1, "VolkswagenGTIReview.mp4"), ep("c8", 2, "ForBiggerBlazes.mp4")),
            setOf(TAG_TOP_VIEWED, TAG_NEW)
        )
    )

    val baseGenres = listOf(
        "Ação", "Animação", "Aventura", "Comédia", "Crime", "Documentário", "Drama", "Fantasia",
        "Ficção científica", "Kids", "Mistério", "Romance", "Terror", "Thriller"
    )
}
