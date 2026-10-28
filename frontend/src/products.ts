export type Product = {
  id: string
  name: string
  category: string
  price: number
  unit: string
  tag: string
  image: string
  description: string
}

export const products: Product[] = [
  {
    id: '11111111-1111-4111-8111-111111111111',
    name: 'Daily Care Essentials',
    category: 'Personal care',
    price: 249,
    unit: 'care kit',
    tag: 'Everyday pick',
    image: 'https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?auto=format&fit=crop&w=720&q=85',
    description: 'Gentle daily-use essentials selected for comfort and dependable care.',
  },
  {
    id: '22222222-2222-4222-8222-222222222222',
    name: 'Home First Aid Kit',
    category: 'First aid',
    price: 499,
    unit: 'kit',
    tag: 'Ready at home',
    image: 'https://images.unsplash.com/photo-1603398938378-e54eab446dde?auto=format&fit=crop&w=720&q=85',
    description: 'A practical assortment of first-aid basics for a well-prepared household.',
  },
  {
    id: '33333333-3333-4333-8333-333333333333',
    name: 'Nourish Hand Cream',
    category: 'Personal care',
    price: 189,
    unit: '100 ml',
    tag: 'Skin comfort',
    image: 'https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?auto=format&fit=crop&w=720&q=85',
    description: 'A rich, non-greasy moisturizer for hands that need a little extra care.',
  },
  {
    id: '44444444-4444-4444-8444-444444444444',
    name: 'Family Wellness Pack',
    category: 'Wellness',
    price: 799,
    unit: 'monthly pack',
    tag: 'Family choice',
    image: 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=720&q=85',
    description: 'A considered bundle of home wellness products for the whole family.',
  },
  {
    id: '55555555-5555-4555-8555-555555555555',
    name: 'Comfort Mobility Support',
    category: 'Elder care',
    price: 649,
    unit: 'support set',
    tag: 'Thoughtful support',
    image: 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?auto=format&fit=crop&w=720&q=85',
    description: 'Useful at-home support items selected with comfort and independence in mind.',
  },
  {
    id: '66666666-6666-4666-8666-666666666666',
    name: 'Clean Living Bundle',
    category: 'Home hygiene',
    price: 359,
    unit: 'bundle',
    tag: 'Home favourite',
    image: 'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=720&q=85',
    description: 'Reliable home hygiene supplies for the spaces your family uses every day.',
  },
]

export const categories = ['All products', ...new Set(products.map((product) => product.category))]