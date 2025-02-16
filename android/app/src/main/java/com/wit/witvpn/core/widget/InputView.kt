package com.wit.witvpn.core.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.wit.witvpn.R
import com.wit.witvpn.core.extension.customViewInflate
import com.wit.witvpn.core.extension.dpToPx
import com.wit.witvpn.core.extension.setFontFamilyHint
import com.wit.witvpn.core.extension.showKeyboard
import com.wit.witvpn.databinding.ViewInputBinding

class InputView(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs), View.OnClickListener {
    companion object {
        const val ACTION_NONE = 0
        const val ACTION_CANCEL = 1
        const val ACTION_HINT = 2
        var STATE_ERROR = intArrayOf(R.attr.state_error)
    }

    private val viewBinding = customViewInflate(ViewInputBinding::inflate)
    private var actionType = ACTION_NONE

    var onTextChanged: ((String) -> Unit)? = null
    val text: String
        get() {
            return viewBinding.editText.text.toString()
        }

    var error: Boolean = false
        set(value) {
            val change = field != value
            field = value
            if (change) {
                if (field) {
                    clearFocus()
                }
                post {
                    refreshDrawableState()
                }
            }
        }

    init {
        setBackgroundResource(R.drawable.bg_input_view)
        val paddingHorizontal = context.dpToPx(16f)
        val paddingVertical = context.dpToPx(8f)
        setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
        initView()
        initAttrs(context, attrs)
        this.setOnClickListener(this)
    }

    private fun initView() {
        viewBinding.editText.setOnFocusChangeListener { _, hasFocus ->
            this.isSelected = hasFocus
            val color = if (hasFocus) {
                this.error = false
                ContextCompat.getColor(context, R.color.white)
            } else {
                ContextCompat.getColor(context, R.color.white)
            }
            viewBinding.editText.setTextColor(color)
            toggleIcon()
        }
        viewBinding.editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                onTextChanged?.invoke(viewBinding.editText.text.toString())
                toggleIcon()
            }
        })
        viewBinding.cbIcon.setOnClickListener {
            when (actionType) {
                ACTION_CANCEL -> viewBinding.editText.setText("")
                ACTION_HINT -> {
                    viewBinding.cbIcon.isActivated = !viewBinding.cbIcon.isActivated
                    viewBinding.editText.transformationMethod = if (viewBinding.cbIcon.isActivated) {
                        null
                    } else {
                        PasswordTransformationMethod()
                    }
                    viewBinding.editText.setSelection(viewBinding.editText.length())
                }
            }
        }
    }

    private fun initAttrs(context: Context?, attrs: AttributeSet?) {
        context?.obtainStyledAttributes(attrs, R.styleable.InputView)
            ?.apply {
                val label = this.getString(R.styleable.InputView_iv_label)
                val hint = this.getString(R.styleable.InputView_iv_hint)
                val inputType = this.getInt(R.styleable.InputView_android_inputType, EditorInfo.TYPE_TEXT_VARIATION_NORMAL)
                actionType = this.getInteger(R.styleable.InputView_iv_action, ACTION_NONE)
                setLabel(label)
                setInputText(inputType, hint)
                setIcon(actionType)
            }
            ?.recycle()

    }

    private fun toggleIcon() {
        val text = viewBinding.editText.text.toString()

        if (actionType == ACTION_NONE) {
            return
        }

        viewBinding.cbIcon.visibility = if (text.isEmpty() || !this.isSelected) View.INVISIBLE else View.VISIBLE
    }

    private fun setLabel(label: String?) {
        viewBinding.tvLabel.text = label
    }

    private fun setInputText(inputType: Int, hint: String?) {
        viewBinding.editText.hint = hint
        viewBinding.editText.inputType = inputType
        viewBinding.editText.setFontFamilyHint(R.font.worksans_regular)
    }

    private fun setIcon(type: Int) {
        val iconRes = when (type) {
            ACTION_CANCEL -> R.drawable.ic_cancel
            ACTION_HINT -> R.drawable.ic_eye_state
            else -> -1
        }

        if (iconRes != -1) {
            viewBinding.cbIcon.setBackgroundResource(iconRes)
        }
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)

        if (error) {
            View.mergeDrawableStates(drawableState, STATE_ERROR)
        }

        return drawableState
    }

    override fun clearFocus() {
        super.clearFocus()
        viewBinding.editText.clearFocus()
    }

    override fun onClick(p0: View?) {
        isSelected = true
        viewBinding.editText.requestFocus()
        context.showKeyboard(viewBinding.editText)
    }

    fun reset() {
        viewBinding.editText.setText("")
        error = false
    }
}