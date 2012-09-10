package com.taskos.task;

import com.taskos.task.ui.Option;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Chuong VO
 * 
 */
public interface UiElement {

    public final class Textview implements UiElement {

        private String text;

        public Textview(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public final class Img implements UiElement {

        private String url;

        public Img(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public final class Select implements UiElement {

        private String name;
        private String value;
        private List<Option> options = new ArrayList<Option>();

        public Select(String name, List<Option> options) {
            this.name = name;
            this.options = options;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Option> getOptions() {
            return options;
        }

        public void setOptions(List<Option> options) {
            this.options = options;
        }
    }

    public final class Input implements UiElement {

        private String type;
        private String name;
        private String value;
        
        public Input(String type, String name) {
            this.type = type;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setValue(String string) {
            this.value = string;
        }
    }
}