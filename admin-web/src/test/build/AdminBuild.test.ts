// @vitest-environment node

import {mkdtemp, readFile, rm} from 'node:fs/promises'
import {tmpdir} from 'node:os'
import {join, resolve} from 'node:path'
import {afterAll, beforeAll, describe, expect, it} from 'vitest'
import {build} from 'vite'

describe('admin production build', () => {
  let outputDirectory: string
  let assetReferences: string[]

  beforeAll(async () => {
    outputDirectory = await mkdtemp(join(tmpdir(), 'shop-admin-build-'))
    await build({
      configFile: resolve('vite.config.ts'),
      build: {outDir: outputDirectory, emptyOutDir: true},
    })

    const index = await readFile(join(outputDirectory, 'index.html'), 'utf8')
    assetReferences = [...index.matchAll(/(?:src|href)="([^"]*assets\/[^"]+)"/g)]
      .map((match) => match[1])
  }, 30_000)

  afterAll(async () => {
    await rm(outputDirectory, {recursive: true, force: true})
  })

  it.each([
    '/admin',
    '/admin/products/42',
    '/admin/orders/ORD-2026-100001/show',
  ])('resolves built assets under /admin/assets/ from %s', (documentPath) => {
    expect(assetReferences.length).toBeGreaterThan(0)

    for (const reference of assetReferences) {
      expect(new URL(reference, `https://shop.example${documentPath}`).pathname)
        .toMatch(/^\/admin\/assets\//)
    }
  })
})
