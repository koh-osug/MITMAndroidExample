# Summary

This module is a demo app for a MITM proxy.

# Compilation

Because submodules are used execute:

    git pull --recurse-submodules
    
Load with Android studio and deploy on the device.
    
# Set-Up

A proxy on `localhost:9092` must be defined:

Go to "Network & internet" -> "WiFi" -> Edit the WiFi connection -> Click the Edit (pencil) icon -> "Advances options"

For mobile data connections the APN settings must be modified to set the proxy.

The LittleProxy-mitm CA must be installed as user credentials (use the file [littleproxy-mitm.pem](app/src/main/assets/littleproxy-mitm.pem).
Go to "Security" -> "Encryption & credentials" -> "Install a certificate"

The Chrome browser is using the user defined CA and will work out of the box. Open Chrome visit a page and check lock icon to inspect the certificate.
Opera has to be patched with a network security configuration. Firefox is not using the proxy settings and is also not obeying a patched network security configuration.

# Test

Open the Chrome browser and surf any web page.

For checking out a client certificate visit (https://client.badssl.com)
