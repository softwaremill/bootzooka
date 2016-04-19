import privateRouting from "./private.routes";

//we are not using angular modules system, it's obsolete while having webpack
export default ngModule => {
  ngModule.config(privateRouting);
};
