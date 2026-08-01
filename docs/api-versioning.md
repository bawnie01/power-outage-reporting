# API Versioning and Compatibility

The public API uses major-version path versioning: `/api/v1`.

Within `v1`, changes must remain backward compatible:

- New optional response properties may be added.
- New optional query parameters may be added.
- Existing property meaning, type, or validation must not change incompatibly.
- Existing enum values must not be removed or renamed.
- Required request properties must not be added.
- Existing status codes and error codes must retain their meaning.

An incompatible change requires `/api/v2`, a migration note, and a defined overlap period. Event envelopes have an independent `eventVersion`; consumers must ignore unknown additive properties. OpenAPI contracts are reviewed in pull requests before implementation changes are merged.
