import { useRouter } from "next/router"
import { useEffect, useState } from "react"

export default function feed() {
  const [posts, setPosts] = useState([])
  const [error, setError] = useState(null)
  const router = useRouter()

  useEffect(() => {
    async function fetchFeed() {
      const token = localStorage.getItem("token")

      if (!token) {
        router.push("/login")
        return
      }

      try {
        const response = await fetch("http://localhost:8080/feed", {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        })

        if (!response.ok) {
          throw new Error("Erro ao obter feed")
        }

        const data = await response.json()
        setPosts(data.feedItens || [])
      } catch (error) {
        console.error("Erro durante a obtenção do feed:", error)
        setError(error.message)
      }
    }

    fetchFeed()
  }, [router])

  return (
    <div>
      <h1>Feed</h1>
      {error && <p>Erro: {error}</p>}
      <ul>
        {posts.map((post) => (
          <li key={post.tweetId}>

            <p>{post.tweetId}</p>
            <p>{post.content}</p>
          </li>
        ))}
      </ul>
    </div>
  )
}
