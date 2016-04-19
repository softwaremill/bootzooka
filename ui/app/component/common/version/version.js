export default ngModule => {
  ngModule.factory('Version', () => {

    class Version {
      constructor(data) {
        this.buildSha = data.build;
        this.buildDate = data.date;
      }

      getBuildSha() {
        return this.buildSha;
      }

      getBuildDate() {
        return this.buildDate;
      }
    }

    return Version;
  });
};
