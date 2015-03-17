package jp.sharakova.android.emoji;

import com.infinity.wefriends.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EmojiEditText extends EditText {
	
	private Context context;
	
	public EmojiEditText(Context context) {
		super(context);
		this.context = context;
	}
	
	public EmojiEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public EmojiEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}
	
	public void setEmojiText(String text) {
		//text = EmojiUtils.convertTag(text);
		CharSequence spanned = Html.fromHtml(text, emojiGetter, null);
		setText(spanned);
		Log.d("WeFriends","text=" + getText().toString());
	}
	
	public String getHtml() {
		return Html.toHtml(getText()).replaceAll("<p.*?>", "").replaceAll("</p>", "");
	}
	
	private ImageGetter emojiGetter = new ImageGetter()
	{
        public Drawable getDrawable(String source){
            // �摜�̃��\�[�XID���擾
            int id = getResources().getIdentifier(source, "drawable", context.getPackageName());
            
            Drawable emoji = getResources().getDrawable(id);
            int w = (int)(emoji.getIntrinsicWidth() * 1.25);
            int h = (int)(emoji.getIntrinsicHeight() * 1.25);
            emoji.setBounds(0, 0, w, h);
            return emoji;
        }
    };

	@Override
	protected void onTextChanged(CharSequence text, int start,
			int lengthBefore, int lengthAfter) {
		try {
			if (text.toString().equals("")) {
				((Button)((Activity)context).findViewById(R.id.chat_send_message_button)).setVisibility(View.GONE);
				((Button)((Activity)context).findViewById(R.id.chat_voice_button)).setVisibility(View.VISIBLE);
			} else {
				((Button)((Activity)context).findViewById(R.id.chat_send_message_button)).setVisibility(View.VISIBLE);
				((Button)((Activity)context).findViewById(R.id.chat_voice_button)).setVisibility(View.GONE);
			}
		} catch (Exception e) {
			
		}
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
	}
    
	public String getFormatedText() {
		return null;
	}
    

}
