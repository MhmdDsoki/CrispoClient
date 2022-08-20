package com.dsoki.crispoclient.Database
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class LocalCartDateSource(private val cartDAO:CartDao) :CartDataSource {
    override fun getAllCart(uid: String): Flowable<List<CartItem>> {
        return cartDAO.getAllCart(uid)}

    override fun countItemInCart(uid: String): Single<Int> {
        return cartDAO.countItemInCart(uid)   }

    override fun sumPrice(uid: String): Single<Double> {
        return cartDAO.sumPrice(uid)}

    override fun getIteminCart(foodId: String, uid: String): Single<CartItem> {
        return cartDAO.getIteminCart(foodId,uid)}

    override fun insertOrReplaceAll(vararg cartItem: CartItem): Completable {
        return cartDAO.insertOrReplaceAll(*cartItem)}

    override fun updateCart(cartItem: CartItem): Single<Int> {
        return cartDAO.updateCart(cartItem)}

    override fun deleteCart(cart: CartItem): Single<Int> {
        return cartDAO.deleteCart(cart)}

    override fun cleanCart(uid: String): Single<Int> {
        return cartDAO.cleanCart(uid)}

    override fun getItemWithAllOptionsInCart(
        uid: String,
        foodId: String,
        foodSize: String,
        foodAddon: String
    ): Single<CartItem> {
        return cartDAO.getItemWithAllOptionsInCart(uid,foodId,foodSize,foodAddon)
    }
}