//
// Build
//

def themeDir = new File(project.basedir as File, 'src/main/hugo-theme')

['install', 'build'].each { command ->
  ant.exec(executable: 'yarn', dir: themeDir, failonerror: true) {
    arg(value: command)
  }
}