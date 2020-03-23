@Library(['private-pipeline-library', 'jenkins-shared@CDI-243_repo-sonatype-com']) _

mavenSnapshotPipeline(
  mavenVersion: 'Maven 3.3.x',
  javaVersion: 'Java 8',
  usePublicSettingsXmlFile: false, // is default value could be omitted
  useEventSpy: false, // turn off artifactsPublisher and some other things that couple the withMaven stuff with the maven version
  iqPolicyEvaluation: { stage ->
    nexusPolicyEvaluation iqStage: stage, iqApplication: 'goodies',
      iqScanPatterns: [[scanPattern: 'scan_nothing']]
  }
)
