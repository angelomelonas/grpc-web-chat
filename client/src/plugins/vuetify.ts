import Vue from "vue";
//@ts-ignore
import Vuetify from "vuetify/lib";
import "vuetify/src/stylus/app.styl";

Vue.use(Vuetify, {
  theme: {
    primary: "#1e88e5",
    secondary: "#66bb6a",
    accent: "#8c9eff",
    success: "#a5d6a7",
    info: "#eceff1",
    warning: "#ffd699",
    error: "#ffab91"
  },
  iconfont: "md"
});
