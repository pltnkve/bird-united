/** @type {import('next').NextConfig} */
const nextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  images: {
    unoptimized: true,
  },
  output: 'standalone',
  async rewrites() {
    return [
      {
        source: '/api/ums/:path*',
        destination: process.env.NEXT_PUBLIC_API_URL || 'http://ums-service:9000/:path*',
      },
      {
        source: '/api/twitter/:path*',
        destination: process.env.NEXT_PUBLIC_TWITTER_API_URL || 'http://twitter-service:9001/:path*',
      },
    ]
  },
}

export default nextConfig
