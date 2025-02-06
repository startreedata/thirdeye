export const getErrorObj = (id:number) => {
  const date = new Date()
  return {
    id: id+1,
    user: `test-${id+1}@error.com`,
    type: 'TypeError',
    message: 'Cannot read properties of the object',
    time: `${date.getMonth()}-${date.getDate()}-${date.getFullYear()}`
  }
}