package com.dsoki.crispoclient.model

class BestDealsModel {
    var food_id:String?=null
    var name:String?=null
    var menu_id:String?=null
    var image:String?=null

    constructor()

    constructor(food_id: String?, name: String?, menu_id: String?, image: String?) {
        this.food_id = food_id
        this.name = name
        this.menu_id = menu_id
        this.image = image
    }


}