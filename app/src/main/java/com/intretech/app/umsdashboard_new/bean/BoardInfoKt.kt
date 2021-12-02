package com.intretech.app.umsdashboard_new.bean

data class BoardInfoKt(
    val boardHomePage: String = "",
    val type: Int = 0        //3
)
/*{      "isExistBoard": 1, //是否存在对应的看板，1存在；0不存在
            "boardHomePage": "/API/MES/Index", //看板完整页面地址，当对应的看板不存在时返回默认页面地址；
            "boardCategoryName": "生产状况看板",  //看板类型名称；
            "boardName": "SMT车间综合看板", //看板名称；
            "macAddress": "C4EB8",  //MAC地址；
            "workShopName": "组装车间", //车间；
            "lineName": "组装02线", //产线；
            "boardNotice": "欢迎光临盈趣汽车电子！！！" //看板通知；
}*/