#!/usr/bin/env bash

cd @project.build.directory@


cp dependency/@engineJar@ classes/launchd-pkg
pkgbuild \
    --root classes/launchd-pkg \
    --scripts      classes/launchd-pkg/scripts \
    --identifier   org.ogerardin.b2b.daemon \
    --version      @project.version@ \
    --ownership    recommended \
    daemon.pkg

pkgbuild \
    --root bundles \
    --install-location /Applications \
    --identifier   org.ogerardin.b2b.icon \
    --version      @project.version@ \
    --ownership    recommended \
    icon.pkg

productbuild \
    --distribution classes/productbuild/product.xml \
    --resources    . \
    --package-path . \
    --version      @project.version@ \
    back2back.pkg

