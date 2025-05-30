/*
 * This file is part of HyperCeiler.
 *
 * HyperCeiler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2023-2025 HyperCeiler Contributions
 */

package com.sevtinge.hyperceiler.hook.module.hook.systemui.statusbar.model

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.telephony.SubscriptionManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.github.kyuubiran.ezxhelper.misc.ViewUtils.findViewByIdName
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.api.Dependency
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.api.MiuiStub
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobileClass.hdController
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobileClass.mOperatorConfig
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobileClass.modernStatusBarMobileView
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobileClass.shadeHeaderController
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobilePrefs.bold
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobilePrefs.fontSize
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobilePrefs.getLocation
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobilePrefs.hideIndicator
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobilePrefs.isEnableDouble
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobilePrefs.leftMargin
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobilePrefs.mobileNetworkType
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobilePrefs.rightMargin
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobilePrefs.showMobileType
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobilePrefs.verticalOffset
import com.sevtinge.hyperceiler.hook.utils.StateFlowHelper.newReadonlyStateFlow
import com.sevtinge.hyperceiler.hook.utils.api.ProjectApi.isDebug
import com.sevtinge.hyperceiler.hook.utils.callMethod
import com.sevtinge.hyperceiler.hook.utils.callMethodAs
import com.sevtinge.hyperceiler.hook.utils.devicesdk.DisplayUtils.dp2px
import com.sevtinge.hyperceiler.hook.utils.devicesdk.SubscriptionManagerProvider
import com.sevtinge.hyperceiler.hook.utils.devicesdk.isMoreSmallVersion
import com.sevtinge.hyperceiler.hook.utils.getBooleanField
import com.sevtinge.hyperceiler.hook.utils.getIntField
import com.sevtinge.hyperceiler.hook.utils.getObjectField
import com.sevtinge.hyperceiler.hook.utils.getObjectFieldAs
import com.sevtinge.hyperceiler.hook.utils.getObjectFieldOrNull
import com.sevtinge.hyperceiler.hook.utils.setObjectField
import java.lang.reflect.Method
import java.util.function.Consumer

object MobileTypeSingle2Hook : BaseHook() {
    private val DarkIconDispatcherClass by lazy {
        loadClass("com.android.systemui.plugins.DarkIconDispatcher", lpparam.classLoader)
    }
    var method: Method? = null
    var method2: Method? = null
    private var get0: Float = 0.0f
    private var get1: Int = 0
    private var get2: Int = 0
    private val simDataConnected = booleanArrayOf(false, false)

    override fun init() {
        hookMobileView()
        if (!showMobileType) return

        try {
            method = DarkIconDispatcherClass.getMethod(
                "isInAreas",
                MutableCollection::class.java,
                View::class.java
            )
            method2 = DarkIconDispatcherClass.getMethod(
                "getTint",
                MutableCollection::class.java,
                View::class.java,
                Integer.TYPE
            )
        } catch (_: Throwable) {
            logE(TAG, lpparam.packageName, "DarkIconDispatcher methods not found")
            method = null
            method2 = null
        }
        if (method == null && method2 == null) {
            return
        }

        findAndHookMethod(
            "com.android.systemui.statusbar.pipeline.shared.ui.view.ModernStatusBarView",
            "onDarkChanged",
            ArrayList::class.java,
            Float::class.java,
            Integer.TYPE,
            Integer.TYPE,
            Integer.TYPE,
            Boolean::class.java,
            object : MethodHook() {
                override fun after(it: MethodHookParam) {
                    if ("mobile" == it.thisObject.getObjectFieldAs<String>("slot")) {
                        get0 = it.args[1] as Float
                        get1 = it.args[3] as Int
                        get2 = it.args[4] as Int
                        val num = it.args[2] as Int
                        val getBoolean = it.args[5] as Boolean
                        val getView = it.thisObject as ViewGroup
                        val textView: TextView =
                            getView.findViewByIdName("mobile_type_single") as TextView

                        if (getBoolean) {
                            method2?.invoke(null, it.args[0], textView, num)?.let { it1 ->
                                textView.setTextColor(it1.hashCode())
                            }
                            return
                        }
                        val getBoolean2 = method?.invoke(null, it.args[0], textView)?.let { it1 ->
                            textView.setTextColor(num)
                        } as Boolean
                        if (getBoolean2) {
                            get0 = 0.0f
                        }
                        if (get0 > 0.0f) {
                            get1 = get2
                        }
                        textView.setTextColor(get1)
                        return
                    }
                }
            })
    }

    private fun hookMobileView() {
        if (mobileNetworkType == 3 || mobileNetworkType == 4 || showMobileType) {
            if (isMoreSmallVersion(200, 2f)) {
                findAndHookMethod("com.android.systemui.statusbar.views.MobileTypeDrawable", "measure", object : MethodHook() {
                    override fun before(param: MethodHookParam?) {
                        param!!.result = null
                    }
                })
            }
        }
        modernStatusBarMobileView.methodFinder()
            .filterByName("constructAndBind")
            .filterByParamCount(5)
            .single().createHook {
                after { param ->
                    val viewModel = param.args[4]
                    val rootView = param.result as ViewGroup
                    val subId = rootView.getIntField("subId")

                    val slotIndex = SubscriptionManager.getSlotIndex(subId)
                    if (slotIndex == -1) {
                        return@after
                    }

                    val mobileGroup = rootView.findViewByIdName("mobile_group") as LinearLayout
                    val containerLeft =
                        mobileGroup.findViewByIdName("mobile_container_left") as ViewGroup
                    val containerRight =
                        mobileGroup.findViewByIdName("mobile_container_right") as ViewGroup

                    // 添加大 5G 并设置样式
                    if (showMobileType) {
                        val mobileType =
                            containerLeft.findViewByIdName("mobile_type") as? ImageView?
                        val textView =
                            mobileGroup.findViewByIdName("mobile_type_single") as TextView
                        if (!getLocation) {
                            mobileGroup.removeView(textView)
                            mobileGroup.addView(textView)
                        }
                        if (fontSize != 27) {
                            textView.textSize = fontSize * 0.5f
                        }
                        if (bold) {
                            textView.typeface = Typeface.DEFAULT_BOLD
                        }
                        val marginLeft = dp2px(leftMargin * 0.5f)
                        val marginRight = dp2px(rightMargin * 0.5f)
                        var topMargin = 0
                        if (verticalOffset != 40) {
                            val marginTop = dp2px((verticalOffset - 40) * 0.1f)
                            topMargin = marginTop
                        }
                        textView.setPadding(marginLeft, topMargin, marginRight, 0)

                        // 大 5G 始终删除小 5G
                        containerLeft.removeView(mobileType)
                    }

                    // 调整初始样式
                    val dataConnected = simDataConnected[slotIndex]
                    if (mobileNetworkType == 3 || mobileNetworkType == 4 || showMobileType) {
                        val paddingLeft = if (dataConnected && !(showMobileType && hideIndicator)) {
                            0
                        } else {
                            if (isMoreSmallVersion(200, 2f)) 0 else 20
                        }

                        containerLeft.setPadding(paddingLeft, 0, 0, 0)
                        containerRight.setPadding(paddingLeft, 0, 0, 0)
                    }

                    if (showMobileType && mobileNetworkType != 4) {
                        // 大 5G 显示逻辑
                        viewModel.setObjectField(
                            "mobileTypeSingleVisible",
                            newReadonlyStateFlow(true)
                        )
                    } else if (!showMobileType) {
                        // 小 5G 显示逻辑
                        viewModel.setObjectField(
                            "mobileTypeVisible",
                            newReadonlyStateFlow(
                                when (mobileNetworkType) {
                                    1, 2 -> true
                                    3 -> false
                                    else -> dataConnected
                                }
                            )
                        )
                    }
                }
            }

        shadeHeaderController.methodFinder()
            .filterByName("onViewAttached")
            .single()
            .createHook {
                after {
                    if ((!hideIndicator && (showMobileType || mobileNetworkType == 3)) || mobileNetworkType == 4) {
                        setOnDataChangedListener()
                    }
                }
            }

        if (showMobileType && mobileNetworkType == 4) {
            showMobileTypeSingle()
        }

        if (showMobileType && (mobileNetworkType == 0 || mobileNetworkType == 2) && !isMoreSmallVersion(200, 2f)) {
            setOnWiFiChangedListener()
        }
    }

    private fun showMobileTypeSingle() {
        mOperatorConfig.constructors[0].createHook {
            after {
                // 启用系统的网络类型单独显示
                // 系统的单独显示只有一个大 5G
                it.thisObject.setObjectField("showMobileDataTypeSingle", true)
            }
        }
    }

    @SuppressLint("NewApi")
    private fun setOnDataChangedListener() {
        val hdController = Dependency.miuiLegacyDependency
            ?.getObjectField("mMiuiIconManagerFactory")
            ?.callMethod("get")
            ?.getObjectField("mMobileUiAdapter")
            ?.getObjectField("hdController")


        val dataConnected =
            hdController?.getObjectFieldOrNull("mMiuiMobileIconsInt")?.getObjectField("dataConnected") ?:
            hdController?.getObjectFieldOrNull("miuiMobileIconsInt")?.getObjectField("dataConnected")

        // 监听移动网络
        MiuiStub.javaAdapter.callMethod(
            "alwaysCollectFlow",
            dataConnected,
            Consumer<BooleanArray> {
                if (isDebug()) logD(TAG, lpparam.packageName, "MobileDataConnected -> ${it.contentToString()}")

                val simCount = it.size
                val isNoDataConnected = when (simCount) {
                    1 -> {
                        simDataConnected[0] = it[0]
                        !it[0]
                    }

                    2 -> {
                        simDataConnected[0] = it[0]
                        simDataConnected[1] = it[1]
                        !it[0] && !it[1]
                    }

                    else -> {
                        simDataConnected[0] = false
                        simDataConnected[1] = false
                        false
                    }
                }
                SubscriptionManagerProvider(EzXHelper.appContext).getActiveSubscriptionIdList(true)
                    .forEach { subId ->
                        getMobileViewBySubId(subId) { view ->
                            val containerLeft =
                                view.findViewByIdName("mobile_container_left") as ViewGroup
                            val containerRight =
                                view.findViewByIdName("mobile_container_right") as ViewGroup

                            val b = (!showMobileType && mobileNetworkType == 4)
                            if (subId == SubscriptionManager.getDefaultDataSubscriptionId()) {
                                if (b) {
                                    val mobileType = view.findViewByIdName("mobile_type") as ImageView
                                    mobileType.visibility = if (isNoDataConnected || showMobileType) {
                                        View.GONE
                                    } else {
                                        View.VISIBLE
                                    }
                                }
                                val paddingLeft = if (isNoDataConnected || (showMobileType && hideIndicator)) {
                                    if (isMoreSmallVersion(200, 2f)) 0 else 20
                                } else {
                                    0
                                }
                                containerLeft.setPadding(paddingLeft, 0, 0, 0)
                                containerRight.setPadding(paddingLeft, 0, 0, 0)
                            } else if (!isEnableDouble) {
                                if (b) {
                                    val mobileType = view.findViewByIdName("mobile_type") as ImageView
                                    mobileType.visibility = View.GONE
                                }
                                containerLeft.setPadding(if (isMoreSmallVersion(200, 2f)) 0 else 20, 0, 0, 0)
                                containerRight.setPadding(if (isMoreSmallVersion(200, 2f)) 0 else 20, 0, 0, 0)
                            }
                        }
                    }
            }
        )
    }

    private fun setOnWiFiChangedListener() {
        hdController.methodFinder().filterByName("update")
            .single().createHook {
                after {
                    val mWifiAvailable = it.thisObject.getBooleanField("mWifiAvailable")

                    if (mWifiAvailable) {
                        setViewVisibility("mobile_type_single", View.GONE)
                    } else {
                        setViewVisibility("mobile_type_single", View.VISIBLE)
                    }
                }
            }
    }

    private fun setViewVisibility(getId: String, visibility: Int) {
        SubscriptionManagerProvider(EzXHelper.appContext).getActiveSubscriptionIdList(true)
            .forEach { subId ->
                getMobileViewBySubId(subId) { view: View ->
                    val mMobileType = view.findViewByIdName(getId) as View
                    mMobileType.visibility = visibility
                }
            }
    }

    private fun getMobileViewBySubId(subId: Int, callback: (View) -> Unit) {
        val statusBarIconController = Dependency.miuiLegacyDependency
            ?.getObjectField("mStatusBarIconController")
            ?.callMethod("get")

        if (statusBarIconController == null) {
            return
        }

        val iconGroups = statusBarIconController.getObjectFieldAs<Map<Any, *>>("mIconGroups")
        val iconList = statusBarIconController.getObjectFieldAs<Any>("mStatusBarIconList")

        val viewIndex = iconList.callMethodAs<Int>("getViewIndex", subId, "mobile")

        iconGroups.forEach { (iconManager, _) ->
            val child = iconManager.getObjectFieldAs<ViewGroup>("mGroup").getChildAt(viewIndex)

            if (child is View &&
                "ModernStatusBarMobileView" == child::class.java.simpleName &&
                "mobile" == child.getObjectField("slot")
            ) {
                callback(child)
            }
        }
    }
}
