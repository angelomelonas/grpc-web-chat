import Vue from "vue";
import Vuetify from "vuetify/lib";

const options = {
  theme: {
    dark: false,
    options: {
      customProperties: true
    },
    themes: {
      light: {
        primary: "#1976D2",
        secondary: "#424242",
        accent: "#82B1FF",
        error: "#FF5252",
        info: "#2196F3",
        success: "#4CAF50",
        warning: "#FFC107"
      },
      dark: {
        primary: "#1976D2",
        secondary: "#424242",
        accent: "#82B1FF",
        error: "#FF5252",
        info: "#2196F3",
        success: "#4CAF50",
        warning: "#FFC107"
      }
    },
    iconfont: "md"
  }
};
Vue.use(Vuetify);

export default new Vuetify(options);
