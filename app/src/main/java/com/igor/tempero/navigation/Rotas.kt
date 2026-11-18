package com.igor.tempero.navigation

/** Todas as rotas (destinos) do app, num lugar só. */
object Rotas {
    // Abas da barra inferior
    const val RECEITAS = "receitas"
    const val COMPRAS = "compras"
    const val PERFIL = "perfil"

    // Telas internas (o id vai na rota; a tela busca o item no banco com ele)
    const val DETALHE_RECEITA = "detalhe_receita/{id}"
    const val DETALHE_ITEM = "detalhe_item/{id}"

    // Formulários: id = -1 cadastra um novo; id de verdade edita (Update)
    const val FORM_RECEITA = "form_receita?id={id}"
    const val FORM_ITEM = "form_item?id={id}"

    fun detalheReceita(id: Int) = "detalhe_receita/$id"
    fun detalheItem(id: Int) = "detalhe_item/$id"
    fun formReceita(id: Int = -1) = "form_receita?id=$id"
    fun formItem(id: Int = -1) = "form_item?id=$id"
}
