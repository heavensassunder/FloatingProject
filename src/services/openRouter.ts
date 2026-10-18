export class OpenRouterClient {
  private apiKey: string;
  private baseUrl = 'https://openrouter.ai/api/v1/chat/completions';

  constructor(apiKey: string) {
    this.apiKey = apiKey.trim();
  }

  async chat(
    systemPrompt: string,
    userPrompt: string,
    model: string = 'openai/gpt-4o-mini',
    maxTokens: number = 200,
    imageBase64?: string
  ): Promise<string> {
    if (!this.apiKey) {
      throw new Error('No API key provided.');
    }

    // Prepare messages array
    const userContent: any = imageBase64
      ? [
          { type: 'text', text: userPrompt },
          {
            type: 'image_url',
            image_url: {
              url: imageBase64.startsWith('data:') ? imageBase64 : `data:image/jpeg;base64,${imageBase64}`,
            },
          },
        ]
      : userPrompt;

    const payload = {
      model,
      max_tokens: maxTokens,
      messages: [
        {
          role: 'system',
          content: systemPrompt,
        },
        {
          role: 'user',
          content: userContent,
        },
      ],
    };

    const response = await fetch(this.baseUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${this.apiKey}`,
        'HTTP-Referer': window.location.origin,
        'X-Title': 'Floating Slime Pet',
      },
      body: JSON.stringify(payload),
    });

    const responseText = await response.text();

    if (!response.ok) {
      let errorMsg = `OpenRouter error ${response.status}`;
      try {
        const errJson = JSON.parse(responseText);
        if (errJson?.error?.message) {
          errorMsg += `: ${errJson.error.message}`;
        } else {
          errorMsg += `: ${responseText}`;
        }
      } catch {
        errorMsg += `: ${responseText}`;
      }
      throw new Error(errorMsg);
    }

    const data = JSON.parse(responseText);
    const content = data?.choices?.[0]?.message?.content;
    if (content) {
      return content.trim();
    }
    if (data?.text) {
      return data.text.trim();
    }
    return '...';
  }
}
