package plus.te4net.core.html;

import plus.te4net.utl.IReplacer;


public final class OnlyHtmlReplacer {
    private String html;
    private int cursor = 0;
    private int type = 0;

    public OnlyHtmlReplacer(String html){
        this.html = html;
    }

    private int getNextIgnore() {
        int index = html.indexOf("<style", cursor);
        int index2 = html.indexOf("<script", cursor);
        if (index == index2) {
            type = 0;
            return html.length();
        }
        if (index < index2 && index != -1) {
            type = 1;
            return index;
        } else {
            type = 2;
            return index2;
        }
    }


    private void updateNextNoIgnore() {
        if (type == 1) {
            int index = html.indexOf("</style>", cursor);
            if (index != -1) {
                cursor = index + 8;
            }
        } else if (type == 2) {
            int index = html.indexOf("</script>", cursor);
            if (index != -1) {
                cursor = index + 9;
            }
        }
    }


    public OnlyHtmlReplacer parse(IReplacer func) {
        int newCursor = getNextIgnore();
        StringBuilder builder = new StringBuilder();
        while (cursor < html.length()) {
            if(cursor!=newCursor) {
                String toReplace = html.substring(cursor, newCursor);
                String result = func.replace(toReplace);
                builder.append(result);
                cursor = newCursor;
            }
            updateNextNoIgnore();
            builder.append(html, newCursor, cursor);
            newCursor = getNextIgnore();
        }
        cursor = 0;
        type = 0;
        html = builder.toString();
        return this;
    }


    public String getHtml(){
        return html;
    }


    public void setHtml(String html){
        this.html = html;
    }
}