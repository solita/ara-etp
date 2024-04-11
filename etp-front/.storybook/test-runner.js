import { waitForPageReady } from '@storybook/test-runner';
import { toMatchImageSnapshot } from 'jest-image-snapshot';

const customSnapshotsDir = `${process.cwd()}/__snapshots__`;

const config = {
  setup() {
    expect.extend({ toMatchImageSnapshot });
  },
  async postVisit(page, context) {
    // use the test-runner utility to wait for fonts to load, etc.
    await waitForPageReady(page);

    console.log('ready');
    // If you want to take screenshot of multiple browsers, use
    // page.context().browser().browserType().name() to get the browser name to prefix the file name
    const image = await page.screenshot();
    expect(image).toMatchImageSnapshot({
      // Default threshold is 0.01, make it bigger so that animated components still pass
      customDiffConfig: { threshold: 0.7 },
      customSnapshotsDir,
      customSnapshotIdentifier: context.id
    });
  }
};

export default config;
