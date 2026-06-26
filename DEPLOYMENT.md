# Deployment Guide

## Publishing to JetBrains Marketplace

This plugin uses GitHub Actions to automatically publish to the JetBrains Marketplace when you push a tag.

### Setup Instructions

#### 1. Get Your JetBrains Marketplace Token

1. Go to https://plugins.jetbrains.com/author/me/tokens
2. Generate a new token with "Marketplace" scope
3. Copy the token value

#### 2. Add Token to GitHub Secrets

1. Go to your GitHub repository → Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Name: `JETBRAINS_MARKETPLACE_TOKEN`
4. Value: Paste your token from step 1
5. Click "Add secret"

### Publishing a New Version

#### Option 1: Push a Tag (Recommended - Fully Automated)

1. Update the version in `gradle.properties`:
   ```properties
   pluginVersion=1.0.1
   ```

2. Commit, tag, and push:
   ```bash
   git add gradle.properties
   git commit -m "Bump version to 1.0.1"
   git tag v1.0.1
   git push && git push --tags
   ```

3. The GitHub Action will automatically:
   - Build the plugin
   - Verify it passes checks
   - Publish to JetBrains Marketplace
   - Create a GitHub release with the plugin ZIP attached
   - Generate release notes from recent commits

#### Option 2: Create a GitHub Release (Manual)

1. Update version in `gradle.properties` and push changes
2. Go to Releases → Create a new release
3. Tag: `v1.0.1`, add release notes
4. Click "Publish release"
5. Workflow triggers automatically

#### Option 3: Manual Trigger

1. Go to Actions → "Publish to JetBrains Marketplace"
2. Click "Run workflow"
3. (Optional) Enter a version override
4. Click "Run workflow"

### Monitoring the Deployment

1. Go to the "Actions" tab in your GitHub repository
2. Click on the running workflow
3. Monitor the build and publish steps
4. Once complete, check https://plugins.jetbrains.com/plugin/YOUR-PLUGIN-ID

### Local Testing Before Publishing

Test the plugin build locally:

```bash
./gradlew buildPlugin
./gradlew verifyPlugin
```

The built plugin will be in `build/distributions/`.

### Troubleshooting

**"Plugin verification failed"**
- Check the verification report in the GitHub Actions logs
- Fix any compatibility issues with target IDE versions

**"Authentication failed"**
- Verify your `JETBRAINS_MARKETPLACE_TOKEN` secret is set correctly
- Regenerate the token if it expired

**"Plugin already exists with this version"**
- Increment the version number in `gradle.properties`
- Each version can only be published once
