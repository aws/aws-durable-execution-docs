// Initialize the self-hosted Mermaid tiny build.
//
// The companion file docs/assets/javascripts/mermaid.tiny.js is a UMD bundle
// that sets window.mermaid when it loads. Zensical's theme integration picks
// up window.mermaid automatically and uses it instead of loading Mermaid from
// unpkg (which is blocked by the Content Security Policy on
// docs.aws.amazon.com).
//
// See CONTRIBUTING.md ("Vendored dependencies") for the upgrade procedure.

if (window.mermaid && typeof window.mermaid.initialize === "function") {
  window.mermaid.initialize({
    startOnLoad: false,
    securityLevel: "strict",
  });
}
