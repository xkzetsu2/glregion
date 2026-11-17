package com.xkzetsu.glregion

import android.os.Build
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class Hook : IXposedHookLoadPackage {
    private val alvos = setOf(
        "com.samsung.android.goodlock",
        "com.sec.android.app.samsungapps" // Galaxy Store (stubDownload.as reenvia o deviceId)
    )
    private val modelo = "SM-S938B"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName !in alvos) return

        XposedHelpers.setStaticObjectField(Build::class.java, "MODEL", modelo)

        try {
            val sem = XposedHelpers.findClass("android.os.SemSystemProperties", lpparam.classLoader)
            XposedBridge.hookAllMethods(sem, "get", object : XC_MethodHook() {
                override fun afterHookedMethod(p: MethodHookParam) {
                    if ((p.args[0] as? String) == "ro.product.model") p.result = modelo
                }
            })
        } catch (t: Throwable) {
            XposedBridge.log("GLRegion: SemSystemProperties indisponível: $t")
        }

        XposedBridge.log("GLRegion: modelo falso $modelo em ${lpparam.packageName}")
    }
}
