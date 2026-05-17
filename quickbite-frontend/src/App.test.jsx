import React from 'react';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import App from './App';

describe('App Component', () => {
  it('renders the QuickBite brand text in the Navbar', () => {
    // App contains the Router internally, so we don't wrap it in BrowserRouter again
    render(<App />);
    const brandElement = screen.getByText(/QuickBite/i);
    expect(brandElement).toBeInTheDocument();
  });
});
